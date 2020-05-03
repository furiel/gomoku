# gomoku

## Compile

Create a standalone jar `target/uberjar/gomoku-0.1.0-SNAPSHOT-standalone.jar`:

```sh
lein uberjar
```

## Deploy

```sh
java -jar target/uberjar/gomoku-0.1.0-SNAPSHOT-standalone.jar
```

You can add TLS for example with `nginx`. To use nginx as a websocket proxy, add

```
location /ws {
    proxy_pass http://localhost:3000/ws;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_read_timeout 1d;
}
```

## Play

Choose an id (for example 1234), and open in browser: http://localhost:3000?id=1234
