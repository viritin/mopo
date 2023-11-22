# Mopo - a helper library for testing Vaadin apps with Playwright

Mopo contains Java helper classes for Playwright users. Especially for more complex Vaadin components, like vaadin-grid, vaadin-date-picker, vaadin-date-time-picker, vaadin-combo-box etc.

To try/use Mopo, add following dependency to your pom.xml (or to Gradle build):

    <dependency>
        <groupId>in.virit</groupId>
        <artifactId>mopo</artifactId>
        <version>0.0.1</version> <!-- check latest version!! ->
        <scope>test</scope>
    </dependency>

Documentation is at this point limited to JavaDoc's. Usage examples can be found from the projects [own IT tests](https://github.com/viritin/mopo/tree/main/src/test/java/firitin/pw).

Currently, Mopo is in very stage so fill in feature requests eagerly. Or fork, develop along your project and create PRs!

Snapshot releases available here: https://oss.sonatype.org/content/repositories/snapshots/

## The name Mopo 🤔

![Mopo](/mopo.png?raw=true "Mopo")

There always needs to be a story behind a name. Mopo is a Finnish word for a moped. Mopeds are small, agile and fast vehicles. And a common moped in Finland used to be Suzuki *PV*, which is almost the shorthand for Playwright :-)

Credits for Marcus Hellberg for using AI to create the logo.
