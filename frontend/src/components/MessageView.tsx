// components/MessageInterface.tsx
import React, {useRef, useState} from 'react';
import { useChordStore } from '../store/store';
import { Message } from '../models/Message';
import { storeMessageInChord, retrieveMessageFromChord } from '../services/ChordService';
import { InputTextarea } from 'primereact/inputtextarea';
import { InputText } from 'primereact/inputtext';
import { Button } from 'primereact/button';
import { Messages } from 'primereact/messages';

const MessageView: React.FC = () => {
    const { selectedNode } = useChordStore();
    const [messageContent, setMessageContent] = useState('');
    const [searchKey, setSearchKey] = useState('');
    const [retrievedMessage, setRetrievedMessage] = useState<Message | null>(null);
    const msgs = useRef(null);

    const handleSendMessage = async () => {
        if (!selectedNode) {
            msgs.current.show({ severity: 'warn', summary: 'Attention', detail: 'Sélectionnez un nœud d\'abord.' });
            return;
        }

        const message: Message = {
            id: '', // L'ID sera généré par le backend
            timestamp: Date.now(),
            author: 'Utilisateur', // Ajustez selon vos besoins
            topic: 'Général',
            content: messageContent,
            data: '',
        };

        const success = await storeMessageInChord(selectedNode, message);
        if (success) {
            msgs.current.show({ severity: 'success', summary: 'Succès', detail: 'Message stocké avec succès.' });
        } else {
            msgs.current.show({ severity: 'error', summary: 'Erreur', detail: 'Échec du stockage du message.' });
        }
    };

    const handleRetrieveMessage = async () => {
        const message = await retrieveMessageFromChord(searchKey);
        if (message) {
            setRetrievedMessage(message);
            msgs.current.show({ severity: 'success', summary: 'Succès', detail: 'Message récupéré avec succès.' });
        } else {
            setRetrievedMessage(null);
            msgs.current.show({ severity: 'info', summary: 'Info', detail: 'Message non trouvé.' });
        }
    };

    return (
        <div>
            <h2>Interface de Message</h2>
            <Messages ref={msgs} />
            <div className="p-field">
                <label htmlFor="messageContent">Contenu du Message</label>
                <InputTextarea
                    id="messageContent"
                    value={messageContent}
                    onChange={(e) => setMessageContent(e.target.value)}
                    rows={5}
                    cols={30}
                />
            </div>
            <Button label="Envoyer le Message" onClick={handleSendMessage} className="p-mt-2" />

            <div className="p-field p-mt-4">
                <label htmlFor="searchKey">Rechercher un Message par Clé</label>
                <InputText
                    id="searchKey"
                    value={searchKey}
                    onChange={(e) => setSearchKey(e.target.value)}
                />
            </div>
            <Button label="Rechercher le Message" onClick={handleRetrieveMessage} className="p-mt-2" />

            {retrievedMessage && (
                <div className="p-mt-4">
                    <h3>Message Récupéré</h3>
                    <p><strong>ID :</strong> {retrievedMessage.id}</p>
                    <p><strong>Auteur :</strong> {retrievedMessage.author}</p>
                    <p><strong>Timestamp :</strong> {new Date(retrievedMessage.timestamp).toLocaleString()}</p>
                    <p><strong>Contenu :</strong> {retrievedMessage.content}</p>
                </div>
            )}
        </div>
    );
};

export default MessageView;
