import React, { useEffect, useState } from 'react';
import { Node } from '../models/Node';
import { getNetworkRing, runNodes, setInitialNodes, stopNode } from '../services/ChordService';
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

    const fetchNodes = async () => {
        const nodes = await getNetworkRing(100);
        setNodes(nodes);
    };

    useEffect(() => {
        fetchNodes();

        const interval = setInterval(() => {
            fetchNodes();
        }, 5000);

        return () => clearInterval(interval);
    }, []);

    const onRowSelect = (event: any) => {
        setSelectedNode(event.data);
        setInitialNodes(event.data.ip, event.data.port);
    };

    const handleRunNodes = async () => {
        const success = await runNodes(nbNodesToRun);
        if (success) {
            toast.current.show({ severity: 'success', summary: 'Success', detail: `${nbNodesToRun} node(s) started.` });
            fetchNodes();
        } else {
            toast.current.show({ severity: 'error', summary: 'Error', detail: 'Failed to start nodes.' });
        }
    };

    const handleStopNode = async (node: Node) => {
        const success = await stopNode(node.ip, node.port);
        if (success) {
            toast.current.show({ severity: 'success', summary: 'Success', detail: `Node ${node.id} stopped.` });
            fetchNodes();
        } else {
            toast.current.show({ severity: 'error', summary: 'Error', detail: `Failed to stop node ${node.id}.` });
        }
    };

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

            <h2>Chord Network</h2>

            <div className="p-field p-grid">
                <label htmlFor="nbNodes" className="p-col-fixed" style={{ width: '100px' }}>Number of nodes:</label>
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
                    <Button label="Start Node" icon="pi pi-plus" onClick={handleRunNodes} />
                </div>
            </div>

            {/* Nodes table */}
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
