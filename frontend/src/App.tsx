import NetworkView from './components/NetworkView.tsx';
import MessageView from "./components/MessageView.tsx";
import NodeView from "./components/NodeView.tsx";

function App() {
    return (
        <div className="p-grid">
            {/* Moitié gauche */}
            <div className="p-col-12 p-md-6">
                <NetworkView />
            </div>
            {/* Moitié droite */}
            <div className="p-col-12 p-md-6">
                {/* Division en deux sections */}
                <div className="p-grid p-dir-col" style={{ height: '100vh' }}>
                    <div className="p-col" style={{ flex: '1 1 auto' }}>
                        <NodeView />
                    </div>
                    <div className="p-col" style={{ flex: '1 1 auto' }}>
                        <MessageView />
                    </div>
                </div>
            </div>
        </div>
    );
}

export default App;