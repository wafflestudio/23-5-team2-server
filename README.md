# 23-5-team2-server
와플스튜디오 23.5기 2조 server

# env

루트 경로에 다음의 `.env` 파일을 작성합니다.

```env
GOOGLE_CLIENT_SECRET=YOUR-GOOGLE-SECRET
```

| Key Name                | Description             |
|-------------------------|-------------------------|
| `GOOGLE_CLIENT_SECRET`  | 구글 OAuth2 Client Secret |

# Auth

- AUTH-TOKEN 쿠키를 사용합니다.

## Google OAuth2

- `/oauth2/authorization/google` 로 GET 요청을 보내면 구글 로그인 과정이 진행됩니다. 
- 로그인 성공 / 실패 모두 프론트엔드 루트 경로로 리다이렉트됩니다.
