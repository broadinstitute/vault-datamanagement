template.project = 'vault-dm'

template.elastic_ip = case environment
when 'ci'
  '54.83.45.229'
when 'production'
  '54.235.155.40'
end

template.conjurtype = "vault-dm"

