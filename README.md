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
