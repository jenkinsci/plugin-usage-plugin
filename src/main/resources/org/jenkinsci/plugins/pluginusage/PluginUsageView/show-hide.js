function showHide(element, btn)
{
    if (element.style.display == 'none')
    {
        element.style.display = '';
        btn.value='collapse';
    }
    else
    {
        element.style.display = 'none';
        btn.value='expand';
    }
}
