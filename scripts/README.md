# Scripts

This directory contains build scripts, deployment scripts, and automation tools for the IAMX repository.

## Current Contents

- `.github/` - CI/CD workflows and GitHub Actions

## Future Additions

This directory will be expanded to include:

- Build scripts for Java utilities
- Deployment scripts for clinic projects
- Data validation scripts
- Testing automation scripts
- Database migration scripts

## Usage

Scripts in this directory are designed to be run from the repository root:

```bash
# Example: Build Java utilities
./scripts/build-java-utilities.sh

# Example: Deploy clinic project
./scripts/deploy-clinic.sh <clinic-name>
```

## Contributing

When adding new scripts:

1. Include clear documentation and usage examples
2. Add error handling and logging
3. Test scripts in a safe environment before committing
4. Follow naming conventions (kebab-case for shell scripts)