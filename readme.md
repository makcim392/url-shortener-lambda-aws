# Description

This is a simple REST API that takes a URL and returns a short URL.

# Endpoints

## POST /

### Request

```json
{
  "body": {
    "originalUrl": "https://example.com",
    "expirationTime": "2024-12-31T23:59:59Z"
  }
}
```

### Response

```json
{
  "code": "12345678"
}
```

# Screenshots

![img.png](img.png)

# License

MIT License © [Rocketseat](https://github.com/rocketseat-education/serverless-rest-api-example)