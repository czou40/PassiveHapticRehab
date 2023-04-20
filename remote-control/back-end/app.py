from flask import Flask, jsonify, request
from flask_socketio import SocketIO, emit
from flask_cors import CORS
app = Flask(__name__)
socketio = SocketIO(app, cors_allowed_origins="*")
CORS(app) # This will enable CORS for all routes

clients = set()

@socketio.on('connect')
def on_connect():
    print('Client connected: ' + (request.sid))
    clients.add(request.sid)

@socketio.on('disconnect')
def on_disconnect():
    print('Client disconnected: ' + (request.sid))
    clients.remove(request.sid)

@app.route("/command", methods=["POST"])
def send_command():
    data = request.get_json()
    id = data["id"]
    command = data["command"]

    if id in clients:
        socketio.emit("command", command, room=id)
        return "Command sent"
    else:
        return "ID not found", 404


if __name__ == '__main__':
    socketio.run(app, debug=True, host='0.0.0.0')