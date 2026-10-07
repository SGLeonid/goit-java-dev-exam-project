# GoIT Java Dev Exam Project

URL Shortener REST API

## API Description

### Authentication end points

* `POST /api/v1/auth/register`
* `POST /api/v1/auth/login`

### Account URL service end points (require authentication)

* `POST /api/v1/account/links`
* `GET /api/v1/account/links?show_expired=false`
* `GET /api/v1/account/links/{id}`
* `PATCH /api/v1/account/links/{id}`
* `DELETE /api/v1/account/links/{id}`

### URL service end point

* `GET /link/{uniqueId}`

For detailed documentation see `/swagger-ui.html` in started web-app

### Environment variables

Environment variables that are used by Docker `compose.yaml`:

* `DB_USER` - The username used for database connection
* `DB_PASSWORD` - The password of database user
* `SECRET_KEY` - The secret key that is used for authentication token generation
* `EXPIRATION_TIME` - The period of time in milliseconds after which a token expires
* `APP_BASE_URL` - The base url for shortened links