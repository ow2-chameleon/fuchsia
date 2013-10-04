Fuchsia
=======

Source Organization
------------------

This folder contains the code source of the OW2 Chameleon Fuchsia project.
- core: This project contains the Fuchsia API and core component.
- core-it: This project contains the integrations tests of the Fuchsia core project.
- distribution: This project help to build Chameleon distributions for Fuchsia.
- examples: This project contains some examples to show how to use Fuchsia.
- fake: This project contains the Fuchsia components providing a way to work with fake device (text file based).
- jaxws: This project contains the Fuchsia components working with the JAX-WS API.
- jsonrpc: This project contains the Fuchsia components working with the JSON-RPC protocol.
- testing: This project contains the Fuchsia testing helpers.
- upnp: This project contains the Fuchsia components working with the UPnP protocol.

License
------------------

Fuchsia is licensed under the Apache License 2.0.


Module hierarchy and naming conventions !Draft!
-----------------------------------------------

### Bases modules

A base is a module which manage dependencies for a protocol. It uses Maven DependencyManagement but can also contains sub-modules which are providing shared bundles in between the fuchsia components of the protocol.

Directory :

    bases/{protocol}

Maven configuration :

```XML
<groupId>org.ow2.chameleon.fuchsia.bases</groupId>
<artifactId>{protocol}</artifactId>
<name>OW2 Chameleon - Fuchsia Base {Protocol}</name>

<parent>
    <groupId>org.ow2.chameleon.fuchsia.bases</groupId>
    <artifactId>FIXME</artifactId>
    <version>{}</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

#### Base sub-modules

Directory :

    bases/{protocol}/{sub-module_name}

Maven configuration :

```XML
<groupId>org.ow2.chameleon.fuchsia.bases</groupId>
<artifactId>FIXME</artifactId>
<name>OW2 Chameleon - Fuchsia Base {Protocol} FIXME</name>

<parent>
    <groupId>org.ow2.chameleon.fuchsia.bases</groupId>
    <artifactId>{protocol}</artifactId>
    <version>{}</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

### Importers modules

#### Importer implementation 

Directory :

    importers/{protocol}

Maven configuration :

```XML
<groupId>org.ow2.chameleon.fuchsia.importers</groupId>
<artifactId>{protocol}</artifactId>
<name>OW2 Chameleon - Fuchsia Importer {Protocol}</name>

<parent>
    <groupId>org.ow2.chameleon.fuchsia.importers</groupId>
    <artifactId>{protocol}</artifactId>
    <version>{}</version>
    <relativePath>../../bases/{protocol}/pom.xml</relativePath>
</parent>
```

#### Importer integration tests

Directory :

    importers/{protocol}-it

Maven configuration :

```XML
<groupId>org.ow2.chameleon.fuchsia.importers</groupId>
<artifactId>{protocol}.it</artifactId>
<name>OW2 Chameleon - Fuchsia Importer {Protocol} [IntegrationTests]</name>

<parent>
    <groupId>org.ow2.chameleon.fuchsia.importers</groupId>
    <artifactId>{protocol}</artifactId>
    <version>{}</version>
    <relativePath>../../bases/{protocol}/pom.xml</relativePath>
</parent>
```

### Discoveries modules

#### Discovery implementation 

Directory :

    discoveries/{protocol}

Maven configuration :

```XML
<groupId>org.ow2.chameleon.fuchsia.discoveries</groupId>
<artifactId>{protocol}</artifactId>
<name>OW2 Chameleon - Fuchsia Discovery {Protocol}</name>

<parent>
    <groupId>org.ow2.chameleon.fuchsia.discoveries</groupId>
    <artifactId>{protocol}</artifactId>
    <version>{}</version>
    <relativePath>../../bases/{protocol}/pom.xml</relativePath>
</parent>
```

#### Discovery integration tests

Directory :

    discoveries/{protocol}-it

Maven configuration :

```XML
<groupId>org.ow2.chameleon.fuchsia.discoveries</groupId>
<artifactId>{protocol}.it</artifactId>
<name>OW2 Chameleon - Fuchsia Discovery {Protocol} [IntegrationTests]</name>

<parent>
    <groupId>org.ow2.chameleon.fuchsia.discoveries</groupId>
    <artifactId>{protocol}</artifactId>
    <version>{}</version>
    <relativePath>../../bases/{protocol}/pom.xml</relativePath>
</parent>
```

### Examples modules

TODO

### Tools modules

#### Tool implementation
Directory :

    tools/{tool}

Maven configuration :

```XML
<groupId>org.ow2.chameleon.fuchsia.tools</groupId>
<artifactId>{tool}</artifactId>
<name>OW2 Chameleon - Fuchsia {Tool}</name>

<parent>
    <groupId>org.ow2.chameleon.fuchsia.tools</groupId>
    <artifactId>{tool}</artifactId>
    <version>{}</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

#### Tool integration tests
Directory :

    tools/{tool}-it

Maven configuration :

```XML
<groupId>org.ow2.chameleon.fuchsia.tools</groupId>
<artifactId>{tool}.it</artifactId>
<name>OW2 Chameleon - Fuchsia {Tool} [IntegrationTests]</name>

<parent>
    <groupId>org.ow2.chameleon.fuchsia.tools</groupId>
    <artifactId>{tool}</artifactId>
    <version>{}</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

Commits convention
------------------

- Make commits of logical units.
    A commit should be one (and just one) logical unit. It should be something that someone might want to patch or revert in its entirety, and never piecewise. If it could be useful in pieces, make separate commits. This will result in short, clear, and concise commit messages. Non-atomic commits make for awful run-on commit messages.

- Check for unnecessary whitespace with "git diff --check" before committing.
- Do not check in commented out code or unneeded files.
- Provide a meaningful commit message.
    The first line of the commit message should be a short description (50 characters is the soft limit, see DISCUSSION in git-commit(1)), and should skip the full stop.  It's possible to prefix the first line with "area: " where the area is a filename or identifier for the general area of the code being modified, e.g.
    - jsonrpc: update jabsorb dependency version
    - testing: Fix fuchsiaHelper dispose in CommonTest.tearDown()

    The body should provide a meaningful commit message, which:
    - explains the problem the change tries to solve, iow, what is wrong with the current code without the change.
    - justifies the way the change solves the problem, iow, why the result with the change is better.
    - alternate solutions considered but discarded, if any.

    Describe your changes in imperative mood, e.g. "make xyzzy do frotz" instead of "[This patch] makes xyzzy do frotz" or "[I] changed xyzzy to do frotz", as if you are giving orders to the codebase to change its behaviour.  Try to make sure your explanation can be understood without external resources. Instead of giving a URL to a mailing list archive, summarize the relevant points of the discussion.

- Make sure that you have tests for the bug you are fixing.
- Make sure that the test suite passes after your commit.
