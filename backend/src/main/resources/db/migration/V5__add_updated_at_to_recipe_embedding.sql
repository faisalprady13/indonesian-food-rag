alter table recipe_embedding
    add column updated_at timestamp(6);

update recipe_embedding set updated_at = created_at where updated_at is null;