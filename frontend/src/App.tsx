import NetworkView from './components/NetworkView.tsx';
import MessageView from "./components/MessageView.tsx";
import NodeView from "./components/NodeView.tsx";
import MeasurementView from "./components/MeasurementView.tsx";

function App() {
    return (
        <div className="p-grid">
            <div className="p-col-12 p-md-6">
                <NetworkView />
            </div>
            <div className="p-col-12 p-md-6">
                <div className="p-grid p-dir-col" style={{height: '100vh'}}>
                    <div className="p-col" style={{flex: '1 1 auto'}}>
                        <NodeView/>
                    </div>
                    <div className="p-col" style={{flex: '1 1 auto'}}>
                        <MessageView/>
                    </div>
                    <div className="p-col" style={{flex: '1 1 auto'}}>
                        <MeasurementView/>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default App;