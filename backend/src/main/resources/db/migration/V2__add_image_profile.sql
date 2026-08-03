ALTER TABLE users
    ADD COLUMN IF NOT EXISTS profile_image_public_id VARCHAR(255);