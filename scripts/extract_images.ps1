$word = New-Object -ComObject Word.Application
$word.Visible = $false
$doc = $word.Documents.Open("D:\43_KLTN_TTFood\UI Grabfood.docx")

# Save as HTML to extract images
$htmlPath = "D:\43_KLTN_TTFood\ui_grabfood_html"
$doc.SaveAs([ref]$htmlPath, [ref]8)  # 8 = wdFormatHTML

$doc.Close([ref]$false)
$word.Quit()

Write-Host "Saved HTML and images to $htmlPath"
