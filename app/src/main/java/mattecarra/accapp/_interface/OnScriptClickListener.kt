package mattecarra.accapp._interface

import mattecarra.accapp.models.AccaScript

// Interface for handling OnClick with a Scriipt

interface OnScriptClickListener
{
    fun onScriptClick(script: AccaScript)
    fun onScriptRunSilent(script: AccaScript)
    fun onEditScript(script: AccaScript)
    fun onCopyScript(script: AccaScript)
    fun onRenameScript(script: AccaScript)
    fun onDeleteScript(script: AccaScript)
}