
import React, { useEffect, useState } from 'react';
import { Node } from '../models/Node';
import {getNetworkRing, runNodes, setInitialNodes, stopNode} from '../services/ChordService';
import { useChordStore } from '../store/store';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { InputNumber } from 'primereact/inputnumber';
import { Button } from 'primereact/button';
import { Toast } from 'primereact/toast';

const NetworkView: React.FC = () => {
    const [nodes, setNodes] = useState<Node[]>([]);
    const { setSelectedNode } = useChordStore();
    const [nbNodesToRun, setNbNodesToRun] = useState<number>(1);
    const toast = React.useRef<any>(null);

    // Fonction pour récupérer la liste des nœuds
    const fetchNodes = async () => {
        const nodes = await getNetworkRing(100); // Ajustez la profondeur selon vos besoins
        setNodes(nodes);
    };

    useEffect(() => {
        fetchNodes();

        // Rafraîchissement automatique toutes les 5 secondes
        const interval = setInterval(() => {
            fetchNodes();
        }, 5000);

        return () => clearInterval(interval); // Nettoyer l'intervalle lors du démontage du composant
    }, []);

    const onRowSelect = (event: any) => {
        setSelectedNode(event.data);
        setInitialNodes(event.data.ip, event.data.port);
    };

    const handleRunNodes = async () => {
        const success = await runNodes(nbNodesToRun);
        if (success) {
            toast.current.show({ severity: 'success', summary: 'Succès', detail: `${nbNodesToRun} nœud(s) lancé(s).` });
            fetchNodes(); // Rafraîchir la liste des nœuds immédiatement
        } else {
            toast.current.show({ severity: 'error', summary: 'Erreur', detail: 'Échec du lancement des nœuds.' });
        }
    };

    const handleStopNode = async (node: Node) => {
        const success = await stopNode(node.ip, node.port);
        if (success) {
            toast.current.show({ severity: 'success', summary: 'Succès', detail: `Nœud ${node.id} arrêté.` });
            fetchNodes(); // Rafraîchir la liste des nœuds
        } else {
            toast.current.show({ severity: 'error', summary: 'Erreur', detail: `Échec de l'arrêt du nœud ${node.id}.` });
        }
    };

    // Template pour le bouton "Stop Node"
    const stopNodeButtonTemplate = (rowData: Node) => {
        return (
            <Button
                label="Stop Node"
                icon="pi pi-times"
                className="p-button-danger"
                onClick={() => handleStopNode(rowData)}
            />
        );
    };

    return (
        <div>
            <Toast ref={toast} />

            <h2>Réseau Chord</h2>

            {/* Section pour lancer des nœuds */}
            <div className="p-field p-grid">
                <label htmlFor="nbNodes" className="p-col-fixed" style={{ width: '100px' }}>Nombre de nœuds :</label>
                <div className="p-col">
                    <InputNumber
                        id="nbNodes"
                        value={nbNodesToRun}
                        onValueChange={(e) => setNbNodesToRun(e.value || 1)}
                        min={1}
                        max={100}
                    />
                </div>
                <div className="p-col-fixed">
                    <Button label="Start Node(s)" icon="pi pi-plus" onClick={handleRunNodes} />
                </div>
            </div>

            {/* Tableau des nœuds */}
            <DataTable
                value={nodes}
                selectionMode="single"
                onRowSelect={onRowSelect}
                dataKey="id"
                paginator
                rows={10}
                responsiveLayout="scroll"
            >
                <Column field="id" header="ID" sortable />
                <Column field="ip" header="IP" sortable />
                <Column field="port" header="Port" sortable />
                <Column header="Action" body={stopNodeButtonTemplate} />
            </DataTable>
        </div>
    );
};

export default NetworkView;
