class Communicator {
  constructor(client) {
    this.client = client;
    this.handlers = new Map();
    this.waitHandlers = new Map();
    this.buffer = "";
    this.client.on("data", (data) => this.process(data));
  }

  process(data) {
    this.buffer += data.toString();
    while (this.buffer.includes("\n")) {
      const nlIdx = this.buffer.indexOf("\n");
      const msg = this.buffer.slice(0, nlIdx);
      this.buffer = this.buffer.slice(nlIdx + 1);
      console.log("Received: " + msg);
      const msgType = msg.includes("|") ? msg.slice(0, msg.indexOf("|")) : msg;
      if (this.waitHandlers.has(msgType)) {
        if (msg.includes("|")) {
          const params = msg.slice(msg.indexOf("|") + 1).split("|");
          this.waitHandlers.get(msgType)(params);
        } else {
          this.waitHandlers.get(msgType)([]);
        }
        this.waitHandlers.delete(msgType);
        return;
      }
      if (this.handlers.has(msgType)) {
        if (msg.includes("|")) {
          const params = msg.slice(msg.indexOf("|") + 1).split("|");
          this.handlers.get(msgType)(params);
        } else {
          this.handlers.get(msgType)([]);
        }
      }
    }
  }

  write(msg) {
    this.client.write(msg);
    console.log("Sent: " + msg);
  }

  on(msgType, handler) {
    this.handlers.set(msgType, handler);
  }

  wait(msgType, handler) {
    this.waitHandlers.set(msgType, handler);
  }
}

export default Communicator;
