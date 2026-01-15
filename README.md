# 23-5-team2-server
와플스튜디오 23.5기 2조 server

# env

루트 경로에 다음의 `.env` 파일을 작성합니다.

| Key Name               | Description               |
|------------------------|---------------------------|
| `GOOGLE_CLIENT_ID`     | 구글 OAuth2 Client ID       |
| `GOOGLE_CLIENT_SECRET` | 구글 OAuth2 Client Secret   |
| `AWS_S3_ACCESS_KEY`    | AWS S3 ACCESS KEY         |
| `AWS_S3_SECRET_KEY`    | AWS S3 SECRET KEY         |

# Auth

- AUTH-TOKEN 쿠키를 사용합니다.

## Google OAuth2

- `/oauth2/authorization/google?redirect_uri=` 로 이동하면 구글 로그인 과정이 진행됩니다. 
- 로그인 성공 / 실패 모두 쿼리 파라미터로 설정한 URL로 리다이렉트됩니다. 

# deploy

- docker, nginx를 설치합니다
- cd-runner 계정을 생성하고 docker 권한을 부여합니다
- 다음의 명령어를 실행하여 mysql을 실행합니다
```sh
docker run -d \
  --name mysql \
  --network app-network \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD='YOUR-ROOT-PW' \
  -e MYSQL_DATABASE=mydatabase \
  -e MYSQL_USER=user \
  -e MYSQL_PASSWORD='YOUR-USER-PW' \
  --restart always \
  mysql:8.0
```
- 다음 nginx 설정 파일을 작성합니다
```nginx
server {
    listen 80;
    server_name yourdomain.com; # Replace with your domain or IP address

    # Optional: Log files for troubleshooting
    access_log /var/log/nginx/reverse-proxy_access.log;
    error_log /var/log/nginx/reverse-proxy_error.log;

    location / {
        # The core redirect logic
        proxy_pass http://127.0.0.1:8080;

        # Standard headers to ensure the backend app gets the correct user info
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Handle WebSocket connections (optional but recommended)
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```
-검색은 키워드 검색으로 진행하였습니다.
