#
# COPYRIGHT Ericsson 2024
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#

import json
from jsonschema import validate, ValidationError
import os


def load_json_file(file_path):
    try:
        with open(file_path, 'r') as file:
            data = json.load(file)
            return data
    except FileNotFoundError:
        print(f"File '{file_path}' not found.")
        return None
    except json.JSONDecodeError as e:
        print(f"Error decoding JSON file: {e}")
        return None


def validate_json_file(json_data, schema):
    try:
        validate(instance=json_data, schema=schema)
        print("JSON file is compliant with the schema.")
    except ValidationError as e:
        print(f"JSON file does not comply with the schema: {e}")


gpr_soc_file_path = os.path.join("..", "doc", "GPR_Compliance", "gprSoc.json")
gpr_soc_schema_file_path = os.path.join("..", "doc", "GPR_Compliance", "gprSocSchema.json")

gpr_soc = load_json_file(gpr_soc_file_path)
gpr_soc_schema = load_json_file(gpr_soc_schema_file_path)

if gpr_soc:
    print("JSON data loaded successfully:")
    validate_json_file(gpr_soc, gpr_soc_schema)

else:
    print("Failed to load JSON data.")