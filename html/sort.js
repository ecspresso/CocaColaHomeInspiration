function filterByBuilding(name) {
    if(name === null) {
        return;
    }

    if(localStorage.getItem("byggnad") === name) {
        resetFilter();
        return;
    }

    localStorage.setItem("byggnad", name);

    let allRows = document.querySelector('tbody').children;

    for(const row of allRows) {
        if(row.getAttribute('data-byggnad') === name) {
            row.removeAttribute("style");
        } else {
            row.style.display = "none";
        }
    }
}

function resetFilter() {
    let allRows = document.querySelector('tbody').children;

    for(const row of allRows) {
        row.removeAttribute("style");
    }

    localStorage.removeItem("byggnad");
}

filterByBuilding(localStorage.getItem("byggnad"))