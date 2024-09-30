import { NodeHeader } from './NodeHeader';
import { FingerTable } from './FingerTable';
import { Message } from './Message';

export interface Node {
    ip: string;
    port: number;
    id: string;
    predecessor: NodeHeader;
    successor: NodeHeader;
    fingerTable: FingerTable;
    m: number;
    messageStore: Message[];
}
