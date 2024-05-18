/**
 * TCP socket message communicator and handler processor
 */
class Communicator {
  constructor(client) {
    this.client = client;
    this.handlers = new Map();
    this.waitHandlers = new Map();
    this.buffer = "";
    this.client.on("data", (data) => this.process(data));
  }

  /**
   * Processes the data received from the server
   * and calls the appropriate handler, if a message is completed and a handler is registered
   * @param data - data received from the server
   */
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

  /**
   * Sends a message to the server
   * @param msg Message
   */
  write(msg) {
    this.client.write(msg);
    console.log("Sent: " + msg);
  }

  /**
   * Registers a handler for a specific message type which is called when a message of that type is received
   * @param msgType Message type
   * @param handler Handler (one parameter - array of message parameters)
   */
  on(msgType, handler) {
    this.handlers.set(msgType, handler);
  }

  /**
   * Registers a disposable handler for a specific message type which is called and forgotten when a message of that type is received
   * @param msgType Message type
   * @param handler Handler (one parameter - array of message parameters)
   */
  wait(msgType, handler) {
    this.waitHandlers.set(msgType, handler);
  }
}

export default Communicator;
