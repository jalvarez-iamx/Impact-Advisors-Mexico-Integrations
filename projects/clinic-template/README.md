# Clinic Template

This is a template directory for setting up a new clinic/hospital project in the IAMX repository.

## Structure

- `cloverdx-workspace/` - CloverDX ETL workspace for this clinic
- `custom-transformations/` - Clinic-specific transformation logic
- `data-mappings/` - Data mapping configurations specific to this clinic
- `config/` - Clinic-specific configuration files
- `docs/` - Documentation for this clinic's implementation

## Setup Instructions

1. Copy this template to a new directory under `projects/`
2. Rename the directory to match the clinic/hospital name
3. Update the README.md with clinic-specific information
4. Configure the CloverDX workspace for the clinic's data sources
5. Add clinic-specific transformations and mappings

## Global Tools Integration

This clinic project can leverage tools from `global-tools/`:

- Java utilities from `global-tools/java-utilities/`
- Shared CloverDX components from `global-tools/cloverdx-shared/`
- Integration guides from `global-tools/docs/`

## Next Steps

- [ ] Set up CloverDX workspace
- [ ] Configure data connections
- [ ] Implement clinic-specific transformations
- [ ] Test data flows
- [ ] Document clinic-specific requirements