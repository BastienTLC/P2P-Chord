// services/ChordService.ts
import apiClient from '../config';
import { Node } from '../models/Node';
import { Message } from '../models/Message';

export const getNetworkRing = async (depth: number): Promise<Node[]> => {
    try {
        const response = await apiClient.get<{ [key: string]: Node }>(`/network/ring/${depth}`);
        const nodesMap = response.data;
        const nodes = Object.values(nodesMap);
        return nodes;
    } catch (error) {
        console.error(error);
        return [];
    }
};

export const storeMessageInChord = async (
    node: Node,
    message: Message
): Promise<boolean> => {
    try {
        const response = await apiClient.post<boolean>(
            `/network/store/${node.ip}/${node.port}`,
            message
        );
        return response.data;
    } catch (error) {
        console.error(error);
        return false;
    }
};

export const retrieveMessageFromChord = async (
    key: string
): Promise<Message | null> => {
    try {
        const response = await apiClient.get<Message>(`/network/retrieve/${key}`);
        return response.data;
    } catch (error) {
        console.error(error);
        return null;
    }
};

export const runNodes = async (nbNodes: number): Promise<boolean> => {
    try {
        const response = await apiClient.patch<boolean>(`/network/run/${nbNodes}`);
        return response.data;
    } catch (error) {
        console.error('Erreur lors du lancement des nœuds :', error);
        return false;
    }
};

// Nouvelle fonction pour arrêter un nœud
export const stopNode = async (host: string, port: number): Promise<boolean> => {
    try {
        const response = await apiClient.patch<boolean>(`/node/stop/${host}/${port}`);
        return response.data;
    } catch (error) {
        console.error('error stopping node:', error);
        return false;
    }
};

export const startTest = async (nbNodes : number, nbMessages: number, dataSize: number, multiThreading: boolean) => {
    try {
        const response = await apiClient.get('/measurements/runTest', {
            params: {
                nbNodes,
                nbMessages,
                dataSize,
                multiThreading,
            },
        });
        return response.data;
    }catch (error) {
        console.error('Error running test:', error);
        return null;
    }
}

    export const setInitialNodes = async (host: string, port: number): Promise<boolean> => {
        try {
            const response = await apiClient.patch<boolean>(`/network/setInitialNodes/${host}/${port}`);
            return response.data;
        } catch (error) {
            console.error('Error setting initial nodes:', error);
            return false;
        }
    }
