# Postman Collection

This folder is intended to store Postman assets for AuthShield API testing.

## Recommended files

- `AuthShield.postman_collection.json` — exported Postman collection for all secured and public endpoints
- `AuthShield.postman_environment.json` — optional environment variables for local testing, for example:
  - `baseUrl` = `http://localhost:8000`
  - `authToken` = `Bearer <token>`

## How to add your Postman export

1. Open Postman.
2. Export the collection for your AuthShield requests.
3. Save the exported JSON file to this folder.
4. Commit the file to your repository so the collection is available to teammates.

## Notes

- Keep sensitive tokens out of committed files.
- Use environment variables for dynamic values like `baseUrl` and `authToken`.
