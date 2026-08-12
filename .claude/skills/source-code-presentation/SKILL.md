Source Code Presentation

Goal

Turn an existing full-stack application's source code into a simple presentation page that is understandable to a mixed technical/non-technical audience.

The page should answer:

What does the application do?

What are its main user-facing features?

How does the application work at a high level?

Why were the important technical approaches chosen?

What are the key frontend, backend, data, and integration pieces?

What is a useful example of a request/user flow through the system?

Do not turn the page into a code documentation dump. Focus on the story of the application.

Inputs

The user may provide:

A repository or source-code directory

A Git repository

Individual source files

Existing documentation

Screenshots or assets

A requested audience or presentation style

If the source is incomplete, make the best evidence-based explanation possible and clearly label assumptions.

Analysis workflow

Before writing the presentation:

1. Map the project

Inspect:

package manifests and lockfiles

README and documentation

frontend entry points and routing

backend entry points and API routes

database/schema/migrations

authentication and authorization

external services/integrations

state management

important shared types/utilities

tests

deployment/configuration files

Build a mental model of:

User → UI → API/server → business logic → database/external services → response → UI

Adapt this model to the actual application.

2. Identify features

Infer user-facing features from:

routes/pages

components

API endpoints

forms/actions

database entities

integrations

permissions

tests

Prioritize features that are visible or meaningful to a product demo.

For every major feature, explain:

What: what the user can do

How: the relevant technical flow

Why: why the implementation matters

Avoid claiming functionality that is not supported by the code.

3. Identify technical architecture

Summarize only the architecture that helps explain the application.

Look for:

framework(s)

frontend structure

backend/API style

persistence layer

authentication

validation

caching

background jobs

file/object storage

third-party APIs

error handling

testing

deployment/runtime

Prefer concrete references to the repository over generic framework descriptions.

4. Find important technical decisions

Select roughly 3–6 decisions that are worth presenting.

Examples:

why server-side vs client-side data fetching is used

why a particular API structure exists

why data is normalized or denormalized

why authentication is handled in a particular layer

why a shared type/schema is used

why optimistic updates, caching, queues, or transactions are used

Only describe a "why" when it can be reasonably inferred from the implementation. If the exact motivation is unknown, phrase it as:

"This approach helps because…"

rather than pretending the original developer explicitly chose it for that reason.

5. Choose one representative flow

Pick one feature that demonstrates the architecture well.

Explain it as a short sequence, for example:

User submits form→ Frontend validates→ API receives request→ Server checks authorization→ Business logic updates database→ API returns result→ UI updates

Use the actual project structure and names where useful.

Presentation structure

Create a single self-contained HTML page with these sections:

Hero

application name

one-sentence description

small technology/architecture summary

What the app does

3–6 concise feature cards

How it works

simple architecture diagram made with HTML/CSS/SVG

avoid complicated diagrams

Feature deep dive

2–4 important features

each with What / How / Why

Technical decisions

3–6 concise decision cards

Example request/user flow

visual step-by-step flow

Tech stack

grouped into Frontend / Backend / Data / Infrastructure / Integrations as applicable

Takeaways

3–5 points a presenter can say verbally

Do not include every repository file. The goal is a presentation, not an API reference.

Design requirements

The result must be a simple presentation page, not a generic dashboard.

Use:

one HTML file

embedded CSS

minimal or no JavaScript

responsive layout

clear typography

generous whitespace

cards and simple diagrams

subtle visual hierarchy

accessible contrast

short paragraphs

code snippets only when they make an architectural point

Do not require a build step or external dependencies unless the user explicitly asks for them.

Use CSS/SVG for diagrams instead of external diagram libraries.

The page should work by opening the HTML file directly in a browser.

Content rules

Ground claims in the source code.

Distinguish confirmed facts from reasonable inference.

Explain technical concepts in plain language first.

Mention implementation details only when they support the story.

Prefer "The frontend calls X, which then..." over vague statements such as "The system is scalable."

Do not invent performance numbers, user counts, business impact, or architectural motivations.

Do not expose secrets, credentials, tokens, private keys, or sensitive environment values found in the repository.

Never copy large sections of source code into the presentation.

If configuration contains secrets, describe the integration without reproducing the secret.

Audience adaptation

If the user specifies an audience, adapt the balance:

Product / non-technical audience

Emphasize:

user value

features

simple flows

why the architecture enables the feature

Minimize:

framework internals

implementation jargon

file paths

Developer / technical audience

Emphasize:

architecture

data flow

API boundaries

state/data management

important implementation choices

representative code snippets

Mixed audience

Use plain-language explanations with a small amount of concrete technical detail. This is the default.

Output

Create:

presentation.html

The HTML must be self-contained and open directly in a browser.

At the end of the work, provide a short summary containing:

the main features discovered

the architecture in one sentence

the 3–6 technical decisions selected

any assumptions or areas where the source code was ambiguous

If the environment supports creating files, also make the generated HTML available as a file to the user.

Quality checklist

Before finishing, verify:

Every major feature is supported by source-code evidence.

The architecture diagram reflects the actual application.

Technical explanations answer both "how" and "why".

No secrets or sensitive configuration values are exposed.

The page is readable without a build tool.

The page works on desktop and mobile widths.

The page is concise enough to present in roughly 5–10 minutes.

The technical stack is accurate.

Inferences are clearly phrased as inferences.

The final page looks like a presentation, not raw documentation.