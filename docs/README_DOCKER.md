# Run Sliding Work Sharing with Docker

## 1. Build the Docker image

From the directory containing the `Dockerfile`, run:

```bash
docker build -t sliding-work-sharing .
```

## 2. Run the default scenario

The release JAR includes the default application configuration and example rules.

Run the application with:

```bash
docker run --rm -p 8080:8080 sliding-work-sharing
```

## 3. Run a custom scenario using mounted YAML and FCL files

To use Sliding Work Sharing with your own application scenario, create a custom FCL rules file and a custom YAML
configuration file.

### Create your custom `.fcl` file

- `fcl` (fuzzy control language) is used to define input parameters, output parameter and decision rules.
- our suggestion would be to take one of the existing `.fcl` files as template and adjust it to your scenario
- existing example `.fcl` files can be found at [src/main/resources/rules](src/main/resources/rules)

_Note_: Please note that the application right now only supports a single output parameter (defined as VAR_OUTPUT in the
.fcl file).

### Create your custom `.yml` configuration file

- our suggestion would be to take an existing `application-{existing-configuration}.yml` as template and adjust it:
    - the `fclRulesFilePath` should point to the location of your `.fcl` file
    - the textual description of the decision results should fit to your scenario
    - replace `{existing-configuration}` with a name representing your custom scenario
- existing example configuration files can be found at [src/main/resources](src/main/resources)

### Prepare the configuration directory

Create a directory on your host machine containing both custom configuration files.

For example:

```text
<YOUR_CONFIG_DIRECTORY>/
├── your-scenario.fcl
└── application-{your-configuration-name}.yml
```

> **Important:** In the YAML configuration, `fclRulesFilePath` must reference the path **inside the container**, not the
> path on the host machine. The `fclRulesFilePath` should be start with `/config/` and then the name of your `.fcl`
> file.

For example:

```yaml
application-scenario-config:
  fclRulesFilePath: /config/your-scenario.fcl
  decisionResultsDescription:
  # Add your scenario-specific descriptions here.
```

The `/config` directory corresponds to the directory mounted inside the Docker container.

## 4. Run the application with the custom configuration

### Linux or macOS

```bash
docker run --rm \
  -p 8080:8080 \
  --mount type=bind,source="<PATH_TO_YOUR_CONFIG_DIRECTORY>",target=/config,readonly \
  sliding-work-sharing \
  --spring.config.location=file:/config/application-{your-configuration-name}.yml
```

Replace:

* `<PATH_TO_YOUR_CONFIG_DIRECTORY>` with the absolute path to the directory containing your YAML and FCL files.
* `{your-configuration}` with the name of your custom scenario.

For example:

```bash
docker run --rm \
  -p 8080:8080 \
  --mount type=bind,source="/home/user/sws-config",target=/config,readonly \
  sliding-work-sharing \
  --spring.config.location=file:/config/application-sws-scenario.yml
```

### Windows PowerShell

```powershell
docker run --rm `
  -p 8080:8080 `
  --mount type=bind,source="<PATH_TO_YOUR_CONFIG_DIRECTORY>",target=/config,readonly `
  sliding-work-sharing `
  --spring.config.location=file:/config/application-{your-configuration-name}.yml
```

Replace:

* `<PATH_TO_YOUR_CONFIG_DIRECTORY>` with the absolute path to the directory containing your YAML and FCL files.
* `{your-configuration-name}` with the name of your custom scenario.

For example:

```powershell
docker run --rm `
  -p 8080:8080 `
  --mount type=bind,source="C:\Users\YourName\sws-config",target=/config,readonly `
  sliding-work-sharing `
  --spring.config.location=file:/config/application-sws-scenario.yml
```

The `readonly` option mounts the configuration directory as read-only inside the container.

## 5. Test the custom scenario

To test your custom scenario, follow the example in the [Testing the application](#how-to-test-the-application) section
and adjust the input parameters to match the parameters defined in your `.fcl` file.
