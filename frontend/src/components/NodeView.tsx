// components/NodeInfo.tsx
import React from 'react';
import { useChordStore } from '../store/store';
import { Node } from '../models/Node';
import { Card } from 'primereact/card';

const NodeView: React.FC = () => {
    const { selectedNode } = useChordStore();

    if (!selectedNode) {
        return <div>Select node neetwork</div>;
    }

    const node: Node = selectedNode;

    return (
        <div>
            <h2>Informations du Nœud</h2>
            <Card>
                <p><strong>ID :</strong> {node.id}</p>
                <p><strong>IP :</strong> {node.ip}</p>
                <p><strong>Port :</strong> {node.port}</p>
                <p><strong>m :</strong> {node.m}</p>

                <h3>Prédécesseur</h3>
                <p><strong>ID :</strong> {node.predecessor?.id}</p>
                <p><strong>IP :</strong> {node.predecessor?.ip}</p>
                <p><strong>Port :</strong> {node.predecessor?.port}</p>

                <h3>Successeur</h3>
                <p><strong>ID :</strong> {node.successor?.id}</p>
                <p><strong>IP :</strong> {node.successor?.ip}</p>
                <p><strong>Port :</strong> {node.successor?.port}</p>

                <h3>Table de Doigts</h3>
                <ul>
                    {node.fingerTable.finger.map((finger, index) => (
                        <li key={index}>
                            <strong>Index {index} :</strong> ID: {finger.id}, IP: {finger.ip}, Port: {finger.port}
                        </li>
                    ))}
                </ul>
            </Card>
        </div>
    );
};

export default NodeView;
