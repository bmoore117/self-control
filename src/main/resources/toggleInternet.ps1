param (
    [switch] $enableInternet = $false
)

$adapters = Get-NetAdapter
$internetAdapters = $adapters | Where-Object {$_.Name -notin "vEthernet (Default Switch)", "Bluetooth Network Connection"}
Foreach ($adapter in $internetAdapters) {
    if ($enableInternet) {
        Enable-NetAdapter -Name $adapter.Name -Confirm:$false
    } else {
        Disable-NetAdapter -Name $adapter.Name -Confirm:$false
    }
}