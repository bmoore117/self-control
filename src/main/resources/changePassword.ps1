param (
    [Parameter(Mandatory=$true)]
    [string] $password
)

$secureString = ConvertTo-SecureString $password -AsPlainText -Force
Get-LocalUser -Name "*local" | Set-LocalUser -Password $secureString
if ($?) {
    exit 0
} else {
    exit 1
}