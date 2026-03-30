variable "region" {
  description = "region"
  default     = "ap-northeast-2"
}

variable "prefix" {
  description = "Prefix for all resources"
  default     = "terra"
}
//도메인은 바꿔야함
variable "app_1_domain" {
  description = "app_1 domain"
  default     = "api.p-14044-1.oa.gg"
}