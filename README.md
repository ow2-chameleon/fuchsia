# Fuchsia


If you want to have more information about fuchsia, visit our [website](http://ow2-chameleon.github.io/fuchsia/).

## Source Organization

This folder contains the code source of the OW2 Chameleon Fuchsia project.
- core: Fuchsia API and core component.
- core-it: Integrations tests of the Fuchsia core project.
- distribution: Help to build Chameleon distributions for Fuchsia.
- examples: Some examples to show how to use Fuchsia.
- bases: Commons modules and dependencies for each protocol used in Fuchsia components.
- importers: Fuchsia importers, responsible to be linked with an import declaration (ID) and convert it to a proxy, or use the ID to provide a service.
- exporters: Fuchsia exporters, in addition to what is done by the importer, the proxy are available outside the OSGi platform.
- discoveries: Fuchsia discoveries, responsible for publishing the import declaration.
- testing: Fuchsia testing helpers.
- tools: Additional tools to facilitate the fuchsia usage. e.g. a shell, plugging, etc.

## License

Fuchsia is licensed under the Apache License 2.0.


## iPOJO usage conventions

Fuchsia rely heavily on iPojo. Each Fuchsia component is an iPOJO component.

Here a few rules to be consistent :

- No component should be instantiated by default, except for the tools,
- No name should be given for the factory, we keep the default name.

## Module hierarchy and naming conventions !Draft!

### Bases modules

A base is a module which manage dependencies for a protocol. It uses Maven DependencyManagement but can also contains sub-modules which are providing shared bundles in between the fuchsia components of the protocol.

Directory :

    bases/{protocol}

Maven configuration :

```XML
<artifactId>org.ow2.chameleon.fuchsia.base.{protocol}</artifactId>
<groupId>org.ow2.chameleon.fuchsia.base</groupId>
<name>OW2 Chameleon - Fuchsia Base {Protocol}</name>

<parent>
    <artifactId>fuchsia-bases</artifactId>
    <groupId>org.ow2.chameleon.fuchsia.bases</groupId>
    <version>{}</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

#### Base sub-modules

Directory :

    bases/{protocol}/{sub-module_name}

Maven configuration :

```XML
<artifactId>org.ow2.chameleon.fuchsia.base.{protocol}.{sub-module_name}</artifactId>
<groupId>org.ow2.chameleon.fuchsia.base.{protocol}</groupId>
<name>OW2 Chameleon - Fuchsia Base {Protocol} : {sub-module_name}</name>

<parent>
    <artifactId>org.ow2.chameleon.fuchsia.base.{protocol}</artifactId>
    <groupId>org.ow2.chameleon.fuchsia.base</groupId>
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
<artifactId>org.ow2.chameleon.fuchsia.importer.{protocol}</artifactId>
<groupId>org.ow2.chameleon.fuchsia.importer</groupId>
<name>OW2 Chameleon - Fuchsia Importer {Protocol}</name>

<parent>
    <artifactId>org.ow2.chameleon.fuchsia.base.{protocol}</artifactId>
    <groupId>org.ow2.chameleon.fuchsia.base</groupId>
    <version>{}</version>
    <relativePath>../../bases/{protocol}/pom.xml</relativePath>
</parent>
<!-- OR -->
<parent>
    <artifactId>fuchsia-importers</artifactId>
    <groupId>org.ow2.chameleon.fuchsia.importer</groupId>
    <version>{}</version>
    <relativePath>../pom.xml</relativePath>
</parent>


```

#### Importer integration tests

Directory :

    importers/{protocol}-it

Maven configuration :

```XML
<artifactId>org.ow2.chameleon.fuchsia.importer.{protocol}-it</artifactId>
<groupId>org.ow2.chameleon.fuchsia.importer</groupId>
<name>OW2 Chameleon - Fuchsia Importer {Protocol} [IntegrationTests]</name>

<parent>
    <artifactId>org.ow2.chameleon.fuchsia.base.{protocol}</artifactId>
    <groupId>org.ow2.chameleon.fuchsia.base</groupId>
    <version>{}</version>
    <relativePath>../../bases/{protocol}/pom.xml</relativePath>
</parent>
<!-- OR -->
<parent>
    <artifactId>fuchsia-importers</artifactId>
    <groupId>org.ow2.chameleon.fuchsia.importer</groupId>
    <version>{}</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

### Discoveries modules

#### Discovery implementation 

Directory :

    discoveries/{protocol}

Maven configuration :

```XML
<artifactId>org.ow2.chameleon.fuchsia.discovery.{protocol}</artifactId>
<groupId>org.ow2.chameleon.fuchsia.discovery</groupId>
<name>OW2 Chameleon - Fuchsia Discovery {Protocol}</name>

<parent>
    <artifactId>org.ow2.chameleon.fuchsia.base.{protocol}</artifactId>
    <groupId>org.ow2.chameleon.fuchsia.base</groupId>
    <version>{}</version>
    <relativePath>../../bases/{protocol}/pom.xml</relativePath>
</parent>
<!-- OR -->
<parent>
    <artifactId>fuchsia-discoveries</artifactId>
    <groupId>org.ow2.chameleon.fuchsia.discovery</groupId>
    <version>{}</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

#### Discovery integration tests

Directory :

    discoveries/{protocol}-it

Maven configuration :

```XML
<artifactId>org.ow2.chameleon.fuchsia.discovery.{protocol}-it</artifactId>
<groupId>org.ow2.chameleon.fuchsia.discovery</groupId>
<name>OW2 Chameleon - Fuchsia Discovery {Protocol} [IntegrationTests]</name>

<parent>
    <artifactId>org.ow2.chameleon.fuchsia.base.{protocol}</artifactId>
    <groupId>org.ow2.chameleon.fuchsia.base</groupId>
    <version>{}</version>
    <relativePath>../../bases/{protocol}/pom.xml</relativePath>
</parent>
<!-- OR -->
<parent>
    <artifactId>fuchsia-discoveries</artifactId>
    <groupId>org.ow2.chameleon.fuchsia.discovery</groupId>
    <version>{}</version>
    <relativePath>../pom.xml</relativePath>
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
<artifactId>fuchsia-{tool}</artifactId>
<groupId>org.ow2.chameleon.fuchsia.tools</groupId>
<name>OW2 Chameleon - Fuchsia {Tool}</name>

<parent>
    <artifactId>fuchsia-tools</artifactId>
    <groupId>org.ow2.chameleon.fuchsia.tools</groupId>
    <version>{}</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

#### Tool integration tests
Directory :

    tools/{tool}-it

Maven configuration :

```XML
<artifactId>fuchsia-{tool}-it</artifactId>
<groupId>org.ow2.chameleon.fuchsia.tools</groupId>
<name>OW2 Chameleon - Fuchsia {Tool} [IntegrationTests]</name>

<parent>
    <artifactId>fuchsia-tools</artifactId>
    <groupId>org.ow2.chameleon.fuchsia.tools</groupId>
    <version>{}</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

## Package naming convention

The package name for the classes of a module must be equal to the artifactId of the module (i.e. for a discovery : "org.ow2.chameleon.fuchsia.discovery.{protocol}").

The tools are the exception, the package name for a tools must be : "org.ow2.chameleon.fuchsia.tools.{tool}".

## Commits convention

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
