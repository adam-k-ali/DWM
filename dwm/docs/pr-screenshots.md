# PR screenshots

GitHub has no API for uploading images into a PR description, so client-visible changes host their
screenshots in a public S3 bucket served through CloudFront and embed plain image URLs.

## Setup (owner, one-time)

1. Deploy [`dwm/infra/screenshots-bucket.yaml`](../infra/screenshots-bucket.yaml) in `eu-west-2`
   (bucket `adam-public-dev-assets`, CloudFront + Origin Access Control, no expiry).
2. Note the `PublicBaseUrl` output (`https://<id>.cloudfront.net`) and set `DWM_ASSETS_BASE_URL`.
3. Repo assets live under the `dwm-repo/` prefix; PR screenshots under
   `dwm-repo/pr-screenshots/pr-<N>/<random>/<name>.png`.

The bucket is private. Only CloudFront can read it; writes are not granted by the template, so only IAM
identities in the account (the owner, or presigned URLs they mint) can upload.

## Workflow

1. Capture screenshots: `./dwm/scripts/run-client-gametest.sh`.
2. **Local:** `./dwm/scripts/upload-pr-screenshots.sh <pr-number>` (uses your AWS credentials).
3. **Claude cloud session:**
   1. `./dwm/scripts/upload-pr-screenshots.sh --plan <pr-number>` lists the object keys.
   2. Mint a presigned PUT URL per key with the AWS MCP `get_presigned_url`, write `name.png=<url>` lines to a file.
   3. `./dwm/scripts/upload-pr-screenshots.sh --url-map <file> <pr-number>` uploads with curl.
      Export the same `DWM_UPLOAD_ID` for steps 1 and 3 so keys match.
   The session network policy must allow `adam-public-dev-assets.s3.eu-west-2.amazonaws.com`.
4. Paste the printed markdown into the PR description's **Screenshots** section.

## Caveats

- Assets are public and permanent (no expiry). Do not upload anything sensitive or unreleased.
- The random path segment keeps URLs unguessable but is not access control.
