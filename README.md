# Event Registration API — Lab 8

JDK 21 (tested with 21.0.2), Gradle Wrapper 8.14, Spring Boot 3.5.16,
PostgreSQL (tested with 16.16). One Event entity and one events table.

## Setup and startup

In pgAdmin: Databases → Create → Database → event_db → Save.
Alternatively, run this in SQL Shell while connected to postgres:

```sql
CREATE DATABASE event_db;
```

Connection: localhost:5432/event_db. The database user needs permission to create tables.
src/main/resources/application.properties uses only the `${DB_USER}` and
`${DB_PASSWORD}` placeholders. Use `ddl-auto=update` only for the local learning database.

IntelliJ: Reload All Gradle Projects; set both Project SDK and Gradle JVM to JDK 21.
Run → Edit Configurations → EventRegistrationApplication → Modify options →
Environment variables: set DB_USER and DB_PASSWORD. Do not enable Store as project file.
Run kz.iitu.registration.EventRegistrationApplication.

Alternatively, use PowerShell from the project directory (replace the placeholders
with your values and adjust JAVA_HOME to your JDK installation):

```powershell
$env:JAVA_HOME = 'C:\Users\Acer\.jdks\openjdk-21.0.2'
$env:DB_USER = '<database-user>'
$env:DB_PASSWORD = '<database-password>'
.\gradlew.bat bootRun
```

Build command: `.\gradlew.bat build`. Do not commit real credentials to Git.

## Code structure

RegistrationController handles HTTP requests and validates fields → EventService
performs operations → EventRepository accesses PostgreSQL. Dependencies are injected
through constructors. Event maps to events; the database generates IDs.
POST creates a new object without the client-provided ID. PUT first finds the row
using the path ID and changes only title and location. The JSON ID is ignored.

| Method | Route | Result |
|---|---|---|
| GET | /api/events | 200, list of events |
| GET | /api/events?title=quiz | 200, case-insensitive partial-title search; [] if no matches |
| GET | /api/events/{id} | 200 or 404 |
| POST | /api/events | 201, event with a database-generated ID |
| PUT | /api/events/{id} | 200 or 404 |
| DELETE | /api/events/{id} | 204 with no body, or 404 |

POST and PUT return 400 without writing to the database if title or location is
missing, null, empty, or whitespace-only.
Example JSON: `{"title":"JavaScript Quiz","location":"Room 202"}`.

## Demonstration with curl in PowerShell

Start the application. Use curl.exe explicitly. Passing JSON through stdin
avoids quotation-mark issues in Windows PowerShell.

```powershell
$base = 'http://localhost:8080/api/events'
curl.exe -i $base
'{"title":"JavaScript Quiz","location":"Room 202"}' | curl.exe -i -X POST $base -H 'Content-Type: application/json' --data-binary '@-'
```

Expect 201. Replace 2 with the returned ID (deleted IDs do not have to be reused):

```powershell
$eventId = 2
curl.exe -i "$base/$eventId"
'{"id":999999,"title":"Updated QUIZ","location":"Room 303"}' | curl.exe -i -X PUT "$base/$eventId" -H 'Content-Type: application/json' --data-binary '@-'
curl.exe -i "$base/$eventId"
curl.exe -i "${base}?title=quiz"
curl.exe -i "${base}?title=no_match_abcdefgh"
```

Expect 200; PUT preserves the path ID; the last request returns [].
Open Query Tool for event_db in pgAdmin, or run `\c event_db` in SQL Shell:

```sql
SELECT id, title, location FROM events;
```

Check that the row matches the API response. Stop the application and start it
again with the same environment variables. In PowerShell:

```powershell
curl.exe -i "$base/$eventId"
```

Expect 200 and the same data. If you have administrator rights, you can stop the
application, restart the local PostgreSQL service through services.msc, start the
application, and repeat GET and SQL. Do not do this on someone else's shared database.

Check invalid input and an unknown ID:

```powershell
'{"title":" ","location":"Room"}' | curl.exe -i -X POST $base -H 'Content-Type: application/json' --data-binary '@-'
'{"title":"Valid"}' | curl.exe -i -X POST $base -H 'Content-Type: application/json' --data-binary '@-'
'{"title":"Valid","location":" "}' | curl.exe -i -X PUT "$base/$eventId" -H 'Content-Type: application/json' --data-binary '@-'
curl.exe -i "$base/9223372036854775807"
'{"title":"Valid","location":"Room"}' | curl.exe -i -X PUT "$base/9223372036854775807" -H 'Content-Type: application/json' --data-binary '@-'
curl.exe -i -X DELETE "$base/9223372036854775807"
```

The first three requests return 400; the next three return 404.
The SQL query should show the unchanged row. Delete the event:

```powershell
curl.exe -i -X DELETE "$base/$eventId"
curl.exe -i "$base/$eventId"
```

Expect 204 with no body, then 404. Repeat the SQL query: the deleted row is gone.
In pgAdmin, find the table under Schemas → public → Tables → events (Refresh).

## Checks actually performed

23 November 2026: Gradle build passed; there are no automated JUnit tests.
HTTP checks were performed through PowerShell on port 18080, and SQL checks through psql:
POST 201, GET/PUT 200, client-provided IDs ignored, quiz matching QUIZ,
empty search result [], unknown GET/PUT/DELETE 404, and POST/PUT with missing,
null, or whitespace-only fields returning 400 without changing the row.
The event with ID 1 survived stopping and restarting the application.
DELETE returned 204 with no body; subsequent GET/DELETE returned 404.
SQL confirmed an empty table. The test record was removed.
curl.exe GET and POST with an invalid body were also checked (200 and 400);
most HTTP checks used Invoke-WebRequest. PostgreSQL was not restarted because
the execution environment could not access Windows Service Control Manager.
Perform that step and capture pgAdmin screenshots locally.

## GitHub

The project includes src, build.gradle, settings.gradle, gradlew, gradlew.bat,
gradle/wrapper/gradle-wrapper.jar and gradle-wrapper.properties, README.md, and .gitignore.
application.properties provides example configuration with placeholders.
build, .gradle, .idea, .run, *.iml, .env, and local application-local.* files are excluded.
Repository: https://github.com/erathespike-oss/Lab_8_web
Before publishing, check `git status` and do not force-add local settings.
