import React, { useRef, useState } from 'react';
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
            msgs.current.show({ severity: 'warn', summary: 'Warning', detail: 'Please select a node first.' });
            return;
        }

        const message: Message = {
            id: '',
            timestamp: Date.now(),
            author: 'User',
            topic: 'General',
            content: messageContent,
            data: '',
        };

        const success = await storeMessageInChord(selectedNode, message);
        if (success) {
            msgs.current.show({ severity: 'success', summary: 'Success', detail: 'Message stored successfully.' });
        } else {
            msgs.current.show({ severity: 'error', summary: 'Error', detail: 'Failed to store the message.' });
        }
    };

    const handleRetrieveMessage = async () => {
        const message = await retrieveMessageFromChord(searchKey);
        if (message) {
            setRetrievedMessage(message);
            msgs.current.show({ severity: 'success', summary: 'Success', detail: 'Message retrieved successfully.' });
        } else {
            setRetrievedMessage(null);
            msgs.current.show({ severity: 'info', summary: 'Info', detail: 'Message not found.' });
        }
    };

    return (
        <div>
            <h2>Message Interface</h2>
            <Messages ref={msgs} />
            <div className="p-field">
                <label htmlFor="messageContent">Message Content</label>
                <InputTextarea
                    id="messageContent"
                    value={messageContent}
                    onChange={(e) => setMessageContent(e.target.value)}
                    rows={5}
                    cols={30}
                />
            </div>
            <Button label="Send Message" onClick={handleSendMessage} className="p-mt-2" />

            <div className="p-field p-mt-4">
                <label htmlFor="searchKey">Search Message by Key</label>
                <InputText
                    id="searchKey"
                    value={searchKey}
                    onChange={(e) => setSearchKey(e.target.value)}
                />
            </div>
            <Button label="Search Message" onClick={handleRetrieveMessage} className="p-mt-2" />

            {retrievedMessage && (
                <div className="p-mt-4">
                    <h3>Retrieved Message</h3>
                    <p><strong>ID:</strong> {retrievedMessage.id}</p>
                    <p><strong>Author:</strong> {retrievedMessage.author}</p>
                    <p><strong>Timestamp:</strong> {new Date(retrievedMessage.timestamp).toLocaleString()}</p>
                    <p><strong>Content:</strong> {retrievedMessage.content}</p>
                </div>
            )}
        </div>
    );
};

export default MessageView;
