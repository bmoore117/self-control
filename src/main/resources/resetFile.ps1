Param (
    [Parameter(Mandatory=$true)]
    [string] $filePath,
    [Parameter(Mandatory=$true)]
    [int] $processId
)

Write-Host "Received path is: $filePath"
Write-Host "Received pid is: $processId"

$childProcs = Get-WmiObject win32_process | where {$_.ParentProcessId -eq $processId}
ForEach ($child in $childProcs) {
    Stop-Process -Id $child.ProcessId -Force
}
Stop-Process -Id $processId

# read file
$fileContents = Get-Content $filePath -Raw

# get file parent dir name
$fileObj = Get-Item $filePath
Write-Host "Obtained dir: $dir"
$dirName = $fileObj.Directory.FullName
Write-Host "Parent dir name: $dirName"
# delete dir and old file
Remove-Item $dirName -Recurse

# create dir and file again, this has the side effect of clearing all old file permission entries
New-Item -Path $dirName -ItemType Directory
$fileContents | Out-File -FilePath $filePath