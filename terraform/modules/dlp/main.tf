# https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/data_loss_prevention_inspect_template

locals {

  dlp_region = var.region == "eu" ? "europe" : var.region
}

resource "google_data_loss_prevention_inspect_template" "inspection_template" {
  parent = "projects/${var.project}/locations/${local.dlp_region}"
  description = "DLP Inspection template used by the BQ security classifier app"
  display_name = "bq_security_classifier_inspection_template"

  # Info Types configured here must be mapped in the infoTypeName_policyTagName_map variable
  # passed to the main module, otherwise mapping to policy tags will fail.

  inspect_config {

    min_likelihood = "LIKELY"

    dynamic info_types{
      // filter the "standard" info types only
      for_each = [for x in var.classification_taxonomy: x if lookup(x, "info_type_category") == "standard"]

      content {
        name =  lookup(info_types.value, "info_type")
      }
    }

    ### CUSTOM INFOTYPES

    custom_info_types {
      info_type {
        name = "CT_PAYMENT_METHOD"
      }

      likelihood = "LIKELY"

      dictionary {
        word_list {
          words = ["Debit Card", "Credit Card"]
        }
      }
    }

    #### RULE SETS


    rule_set {
      info_types {
        name = "EMAIL_ADDRESS"
      }
      rules {
        exclusion_rule {
          regex {
            pattern = ".+@excluded-example.com"
          }
          matching_type = "MATCHING_TYPE_FULL_MATCH"
        }
      }
    }

    # to include findings text in the results table (e.g. user@domain.com -> EMAIL_ADDRESS)
    include_quote = false
  }
}