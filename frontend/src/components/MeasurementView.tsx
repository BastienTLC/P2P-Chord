import React, { useState } from "react";
import { startTest } from "../services/ChordService.ts";
import { Card } from "primereact/card";
import { InputNumber } from "primereact/inputnumber";
import { InputSwitch } from "primereact/inputswitch";
import { Button } from "primereact/button";
import { ProgressSpinner } from "primereact/progressspinner";
import { JSONTree } from "react-json-tree";
import { CopyToClipboard } from "react-copy-to-clipboard";
import { Toast } from "primereact/toast";

const MeasurementView: React.FC = () => {
    const [nbNodes, setNbNodes] = useState<number>(5);
    const [nbMessages, setNbMessages] = useState<number>(10);
    const [dataSize, setDataSize] = useState<number>(1024);
    const [multiThreadingEnabled, setMultiThreadingEnabled] = useState<boolean>(false);
    const [loading, setLoading] = useState<boolean>(false);
    const [result, setResult] = useState<any>(null);
    const [copied, setCopied] = useState<boolean>(false);
    const toast = React.useRef<any>(null);

    const handleRunTest = async () => {
        setLoading(true);
        setResult(null);
        setCopied(false);

        try {
            const response = await startTest(nbNodes, nbMessages, dataSize, multiThreadingEnabled);
            setResult(response);
            setLoading(false);
        } catch (error) {
            console.error('Error running test:', error);
            setLoading(false);
        }
    };

    const handleDownloadJSON = () => {
        const fileData = JSON.stringify(result, null, 2);
        const blob = new Blob([fileData], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = result.executionTimestamp + '.json';
        link.click();
    };

    const handleCopy = () => {
        setCopied(true);
        if (toast.current) {
            toast.current.show({ severity: 'success', summary: 'Copied!', detail: 'JSON copied to clipboard', life: 3000 });
        }
    };

    return (
        <Card title="Performance Measurement">
            <Toast ref={toast} />
            <div className="p-fluid">
                <div className="p-field">
                    <label htmlFor="nbNodes">Number of Nodes:</label>
                    <InputNumber
                        id="nbNodes"
                        value={nbNodes}
                        onValueChange={(e) => setNbNodes(e.value || 0)}
                        min={1}
                        showButtons
                    />
                </div>
                <div className="p-field">
                    <label htmlFor="nbMessages">Number of Messages:</label>
                    <InputNumber
                        id="nbMessages"
                        value={nbMessages}
                        onValueChange={(e) => setNbMessages(e.value || 0)}
                        min={1}
                        showButtons
                    />
                </div>
                <div className="p-field">
                    <label htmlFor="dataSize">Data Size (bytes):</label>
                    <InputNumber
                        id="dataSize"
                        value={dataSize}
                        onValueChange={(e) => setDataSize(e.value || 0)}
                        min={1}
                        showButtons
                    />
                </div>
                <div className="p-field-checkbox">
                    <label htmlFor="multiThreadingEnabled">Multi-threading Enabled:</label>
                    <InputSwitch
                        checked={multiThreadingEnabled}
                        onChange={(e) => setMultiThreadingEnabled(e.value)}
                    />
                </div>
                <Button
                    label="Run Test"
                    icon="pi pi-check"
                    onClick={handleRunTest}
                    disabled={loading}
                />
            </div>

            {loading && (
                <div className="p-d-flex p-jc-center p-mt-4">
                    <ProgressSpinner />
                </div>
            )}

            {result && (
                <div className="p-mt-4">
                    <h3>Test Result:</h3>
                    <JSONTree data={result} />

                    <div className="p-mt-4">
                        <Button
                            label="Download JSON"
                            icon="pi pi-download"
                            onClick={handleDownloadJSON}
                            className="p-mr-2"
                        />
                        <CopyToClipboard text={JSON.stringify(result, null, 2)} onCopy={handleCopy}>
                            <Button
                                label="Copy JSON"
                                icon="pi pi-copy"
                                className={copied ? 'p-button-success' : ''}
                            />
                        </CopyToClipboard>
                    </div>
                </div>
            )}
        </Card>
    );
};

export default MeasurementView;
