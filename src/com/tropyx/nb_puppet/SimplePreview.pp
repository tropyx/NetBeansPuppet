node 'aaa-hosted' {
  # comment here
  require companysoe

  include java

  $java_version = 'jdk1.7.0_40'
  $arch = 'x86_64'

  java::jdk { $java_version:
    arch => $arch,
  }

  class { 'java::defaultversion':
    version => $java_version,
  }

  class { 'buildeng_aaa':
    type => 'hosted',
  }
}
