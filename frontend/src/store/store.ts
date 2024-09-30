import { create } from 'zustand';
import { Node } from '../models/Node';

interface ChordState {
    selectedNode: Node | null;
    setSelectedNode: (node: Node) => void;
}

export const useChordStore = create<ChordState>((set) => ({
    selectedNode: null,
    setSelectedNode: (node: Node) => set({ selectedNode: node }),
}));
