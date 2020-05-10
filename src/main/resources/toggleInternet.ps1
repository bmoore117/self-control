param (
    [switch] $enableInternet = $false
)

$adapters = Get-NetAdapter
$internetAdapters = $adapters | Where-Object {$_.Name -notin "vEthernet (Default Switch)", "Bluetooth Network Connection"}
Foreach ($adapter in $internetAdapters) {
    if ($enableInternet) {
        if ($adapter.Status -ne "Disabled") {
            Write-Host "Enabling $adapter.Name"
            Enable-NetAdapter -Name $adapter.Name -Confirm:$false
        } else {
            Write-Host "$adapter.Name already enabled"
        }
    } else {
        if ($adapter.Status -eq "Disabled") {
            Write-Host "$adapter.Name already disabled"
        } else {
            Write-Host "Disabling $adapter.Name"
            Disable-NetAdapter -Name $adapter.Name -Confirm:$false
        }
    }
}