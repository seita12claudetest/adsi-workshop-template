import http from "node:http";
import httpProxy from "http-proxy";

const PROXY_PORT = parseInt(process.env.PROXY_PORT || "3000", 10);
const NEXT_PORT = parseInt(process.env.NEXT_PORT || "3001", 10);
const BASE_PATH = "/codeeditor/default/absports/3000";

const proxy = httpProxy.createProxyServer({ target: `http://127.0.0.1:${NEXT_PORT}` });

proxy.on("error", (err, _req, res) => {
  console.error("Proxy error:", err.message);
  if (res.writeHead) {
    res.writeHead(502);
    res.end("Bad Gateway");
  }
});

const server = http.createServer((req, res) => {
  const original = req.url;
  req.url = `${BASE_PATH}${req.url}`;
  console.log(`[proxy] incoming=${original} -> forwarded=${req.url}`);
  proxy.web(req, res);
});

server.listen(PROXY_PORT, "0.0.0.0", () => {
  console.log(`SageMaker proxy listening on :${PROXY_PORT} -> next(:${NEXT_PORT})`);
});
