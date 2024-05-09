from flask import Flask, request
from flask_socketio import SocketIO, emit
from flask_cors import CORS
app = Flask(__name__)
socketio = SocketIO(app, cors_allowed_origins="*")
CORS(app) # This will enable CORS for all routes
import json

clients = set()
client_command_mapping = {}
remote_to_client_mapping = {}
client_to_remote_mapping = {}

@socketio.on('connect')
def on_connect():
    print('Connected: ' + (request.sid))

@socketio.on('disconnect')
def on_disconnect():
    print('Disconnected: ' + (request.sid))
    if request.sid in clients:
        clients.remove(request.sid)
        del client_command_mapping[request.sid]
        if request.sid in client_to_remote_mapping:
            remote_id = client_to_remote_mapping[request.sid]
            del remote_to_client_mapping[remote_id]
            del client_to_remote_mapping[request.sid]
        

@app.route("/command", methods=["POST"])
def send_command():
    data = request.get_json()
    id = data["id"]
    command = data["command"]
    if id in clients:
        socketio.emit("client/command", command, room=id)
        return "Command sent"
    else:
        return "ID not found", 404
    
@socketio.on('client')
def handle_register_client():
    clients.add(request.sid)
    client_command_mapping[request.sid] = set()

    
# listen for incoming messages
@socketio.on('client/add')
def handle_add_command(command):
    id = request.sid
    if id not in clients:
        return 
    client_command_mapping[id].add(str(command))
    # relay to remote control client
    if id not in client_to_remote_mapping:
        return # no remote control client
    socketio.emit('remote', str(json.dumps(list(client_command_mapping[id]))), room=client_to_remote_mapping[id])

@socketio.on('client/remove')
def handle_remove_command(command):
    id = request.sid
    if id not in clients:
        return
    if command in client_command_mapping[id]:
        client_command_mapping[id].remove(command)
    # relay to remote control client
    if id not in client_to_remote_mapping:
        return # no remote control client
    socketio.emit('remote', str(json.dumps(list(client_command_mapping[id]))), room=client_to_remote_mapping[id])

@socketio.on('remote')
def handle_remote_command(id):
    remote_id = request.sid
    if id not in clients:
        print('Error: Client not connected: ' + id)
        return
    remote_to_client_mapping[remote_id] = id
    client_to_remote_mapping[id] = remote_id
    socketio.emit('remote', str(json.dumps(list(client_command_mapping[id]))), room=remote_id)

if __name__ == '__main__':
    # gunicorn -k geventwebsocket.gunicorn.workers.GeventWebSocketWorker  app:app
    import socket
    def get_ip():
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        print(s.getsockname()[0])
        s.close()
    get_ip()
    socketio.run(app, debug=True, host='0.0.0.0', port=5000)