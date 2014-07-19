package com.tropyx.nb_puppet.lint;

/**
 *
 * @author mkleint
 */
public enum LintCheck
{
    RIGHT_TO_LEFT_RELATIONSHIP("Right to left relationship", "--no-right_to_left_relationship-check"),
    AUTOLOADER_LAYOUT("Autoloader layout", "--no-autoloader_layout-check"),
    NAME_CONTAINING_DASH("Names containing dash", "--no-names_containing_dash-check"),
    CLASS_INHERITS_FROM_PARAMS_CLASS("Class inherits from params class", "--no-class_inherits_from_params_class-check"),
    CLASS_PARAMETER_DEFAULTS("Class parameter defaults", "--no-class_parameter_defaults-check"),
    PARAMETER_ORDER("Parameter order", "--no-parameter_order-check"),
    INHERITS_ACROSS_NAMESPACES("Inherits across namespaces", "--no-inherits_across_namespaces-check"),
    NESTED_CLASSES_OR_DEFINES("Nested classes or defines", "--no-nested_classes_or_defines-check"),
    VARIABLE_SCOPE("Variable scope", "--no-variable_scope-check"),
    SLASH_COMMENTS("Slash comments", "--no-slash_comments-check"),
    STAR_COMMENTS("Star comments", "--no-star_comments-check"),
    SELECTOR_INSIDE_RESOURCE("Selector inside resource", "--no-selector_inside_resource-check"),
    CASE_WITHOUT_DEFAULT("Case without default", "--no-case_without_default-check"),
    DOCUMENTATION("Documentation", "--no-documentation-check"),
    DOUBLE_QUOTED_STRINGS("Double quoted strings", "--no-double_quoted_strings-check"),
    ONLY_VARIABLE_STRING("Only variable string", "--no-only_variable_string-check"),
    VARIABLES_NOT_ENCLOSED("Variables not enclosed", "--no-variables_not_enclosed-check"),
    SINGLE_QUOTE_STRING_WITH_VARS("Single quote string with variables", "--no-single_quote_string_with_variables-check"),
    QUOTED_BOOLEANS("Quoted booleans", "--no-quoted_booleans-check"),
    VARIABLE_CONTAINS_DASH("Variable contains dash", "--no-variable_contains_dash-check"),
    HARD_TABS("Hard tabs", "--no-hard_tabs-check"),
    TRAILING_WHITESPACE("Trailing whitespace", "--no-trailing_whitespace-check"),
    CHARS_80("80 chars", "--no-80chars-check"),
    SP2_SOFT_TABS("2sp soft tabs", "--no-2sp_soft_tabs-check"),
    ARROW_ALIGNMENT("Arrow alignment", "--no-arrow_alignment-check"),
    UNQUOTED_RESOURCE_TITLE("Unquoted resource title", "--no-unquoted_resource_title-check"),
    ENSURE_FIRST_PARAM("Ensure first param", "--no-ensure_first_param-check"),
    DUPLICATE_PARAMS("Duplicate params", "--no-duplicate_params-check"),
    UNQUOTED_FILE_MODE("Unquoted file mode", "--no-unquoted_file_mode-check"),
    FILE_MODE("File mode", "--no-file_mode-check"),
    ENSURE_NOT_SYMLINK_TARGET("Ensure not symlink", "--no-ensure_not_symlink_target-check");
    
    private final String displayName;
    private final String disableParam;
 
    LintCheck(String displaName, String disableParam) {
        this.displayName = displaName;
        this.disableParam = disableParam;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisableParam() {
        return disableParam;
    }
    
}
