# Projects

This directory contains clinic and hospital-specific projects. Each clinic/hospital has its own subdirectory with customized implementations.

## Current Projects

- `clinic-template/` - Template for setting up new clinic projects
- `hospital-a/` - Placeholder for Hospital A
- `hospital-b/` - Placeholder for Hospital B
- `clinic-x/` - Placeholder for Clinic X
- `specialty-center/` - Placeholder for Specialty Center

## Project Structure

Each clinic project should follow this structure:

```
clinic-name/
├── cloverdx-workspace/     # CloverDX ETL workspace
├── custom-transformations/ # Clinic-specific transformation logic
├── data-mappings/          # Data mapping configurations
├── config/                 # Clinic-specific configuration
└── docs/                   # Project documentation
```

## Getting Started

1. **For new clinics**: Copy `clinic-template/` to a new directory
2. **For existing clinics**: Use the template structure as a guide
3. **Integration**: Leverage global tools from `../global-tools/`

## Global Tools Integration

All projects can use shared utilities from `../global-tools/`:

- Java utilities for data transformations
- Shared CloverDX components
- Integration guides and documentation

## Development Workflow

1. Set up the CloverDX workspace for your clinic
2. Configure data connections and sources
3. Implement clinic-specific transformations
4. Test data flows and mappings
5. Document clinic-specific requirements and configurations