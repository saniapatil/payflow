*PayFlow
A distributed payment system I built after realizing most "payment app" tutorials stop at the easy part  deduct from A, add to B, done. 
That's not actually the hard part of building something like this. 
The hard part is what happens when a request gets sent twice, or two transfers hit the same account at the same millisecond, or the worker crashes halfway through moving money.
This project is my attempt at actually solving those problems instead of ignoring them.

*What it actually solves
Someone double clicks "Pay" (or their wifi drops and retries). 
I put a unique constraint on the idempotency key at the database level. 
Not just a check in the code before saving an actual DB constraint, because a plain "check then insert" has a race condition if two requests land close enough together.
Two transfers touch the same account at once. Both the sender and receiver get locked in Redis before anything happens, always in the same order (lower user ID gets locked first) no matter who's sending to who. Otherwise you can get two transfers deadlocking each other, or worse, both writing to the same account and one write silently overwriting the other.
The worker dies mid-transfer, or a lock expires while it's still working. Each lock has a random token tied to whoever grabbed it, so if it expires and someone else grabs the same lock, the first process can't accidentally release the second one's lock when it finally finishes. There's also a plain Postgres row lock underneath as backup. If a message keeps failing it retries a few times then goes to a dead letter queue instead of just disappearing.

One rule made all of this possible to reason about: only ONE service is allowed to touch account balances. The API just validates stuff and puts a message on a queue. The worker is the only thing that actually moves money.

*Stack
1.Java 21 + Spring Boot for both backend services
2.Postgres, schema managed with Flyway (only the worker is allowed to migrate it)
3.RabbitMQ for the queue between api and worker
4.Redis for locking and caching balances
5.React + Vite for the frontend
6.Groq (LLM) plus some plain rule-based checks for fraud screening
7.Docker Compose for local dev, Render for actually hosting it
8.Prometheus + Grafana if you want to poke around at metrics
9.GitHub Actions runs a build check on every push

*The two services
1.payflow-api — handles login/signup, checks passwords, runs fraud checks, and creates a transaction row marked PENDING before pushing it to the queue. 
It never touches a balance directly except for giving new users their signup bonus.

2.payflow-worker — the only thing allowed to actually change a balance. Picks messages off the queue, grabs the Redis locks, updates both accounts, writes a ledger entry, done.

3.payflow-frontend — just the UI: sign up, log in, dashboard, send money, add money, see your history.