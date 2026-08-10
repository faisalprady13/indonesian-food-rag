alter table conversation_contexts
    drop constraint fk_conversation_contexts_conversation;

alter table conversation_contexts
    add constraint fk_conversation_contexts_conversation foreign key (conversation_id) references conversations on delete cascade;

alter table conversation_context_recipe_ids
    drop constraint fk_conversation_context_recipe_ids_context;

alter table conversation_context_recipe_ids
    add constraint fk_conversation_context_recipe_ids_context foreign key (conversation_context_id) references conversation_contexts on delete cascade;
