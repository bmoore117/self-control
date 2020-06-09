param (
    [switch] $enableInternet = $false
)

$adapters = Get-NetAdapter
$internetAdapters = $adapters | Where-Object {$_.Name -notin "vEthernet (Default Switch)", "Bluetooth Network Connection"}
Foreach ($adapter in $internetAdapters) {
    $name = $adapter.Name
    if ($enableInternet) {
        if ($adapter.Status -eq "Disabled") {
            Write-Host "Enabling $name"
            Enable-NetAdapter -Name $adapter.Name -Confirm:$false
        }
    } else {
        if ($adapter.Status -ne "Disabled") {
            Write-Host "Disabling $name"
            Disable-NetAdapter -Name $adapter.Name -Confirm:$false
        }
    }
}