## Project Part 4 Feedback

### Final Grade

7/8

### Feedback From Project Part 3

Most feedback was addressed.

### Code Base

All requirements completed and code base is well organized. Data validation is present.

### Documentation

Model classes are well documented with Javadocs. Other claasses are freqeuntly missing comments describing the purpose of the class.

### Test Cases

Unit testing could still be improved. The filtering test was good, but you do not need to test things like getters and setters, or observers that will be notified by code that is not yours. UI tests are greatly improved.

### Object Oriented Design

Main activity should have 0..*, not 1..* relationship with fragments. The main activity does not need fragments to exist. Missing cardinalities on Map/MoodEvent relationship. No relationship between user/iuserDao and dashboard, moodevent,  etc.

### Backlog

Backlog is up to date.

### UI and Storyboard

UI mocks were updated for new features.

### Sprint Planning and Review

Sprint plans and team meeting notes have minimum required information, but should be improved.

### Demo 

Presentation energy needs to be improved, and eye contact with the audience was limited. A more cohesive script would have been beneficial.

### Tool Use

PR descriptions could be improved but otherwise good job.

### Relative Quality

The UI can be unintuitive. If I click the sad emoticon on the main page, when I make a mood it should default to that, but this is not the case. There is also two inputs for social situation during mood creation. Hints and errors to the user should be given using the input text fields, not toasts.
