define service (
  $name       , # (namevar) The name of the service to run.  This name is...
  $ensure     , # Whether a service should be running.  Valid...
  $binary     , # The path to the daemon.  This is only used for...
  $control    , # The control variable used to manage services...
  $enable     , # Whether a service should be enabled to start at...
  $flags      , # Specify a string of flags to pass to the startup
  $hasrestart , # Specify that an init script has a `restart...
  $hasstatus  , # Declare whether the service's init script has a...
  $manifest   , # Specify a command to config a service, or a path
  $path       , # The search path for finding init scripts....
  $pattern    , # The pattern to search for in the process table...
  $provider   , # The specific backend to use for this `service...
  $restart    , # Specify a *restart* command manually.  If left...
  $start      , # Specify a *start* command manually.  Most...
  $status     , # Specify a *status* command manually.  This...
  $stop       , # Specify a *stop* command...
  # ...plus any applicable metaparameters.
){}