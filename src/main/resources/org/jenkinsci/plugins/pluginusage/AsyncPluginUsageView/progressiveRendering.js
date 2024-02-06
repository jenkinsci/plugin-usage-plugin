function display(r) {

    const factors = document.getElementById("factors");
    const plugins = r.plugins
    const jobs = r.jobs

    for(var i = 0; r.correlations.length > i; i++){
        const pluginName = r.correlations[i].plugin
        const jobNames = r.correlations[i].jobs

        if (document.getElementById(pluginName) == null){
            const li = document.createElement("li");
            li.setAttribute('id', pluginName);

            const a = document.createElement("a");
            a.setAttribute("href", plugins[pluginName].url);
            a.appendChild(document.createTextNode(plugins[pluginName].displayName));
            li.appendChild(a);

            const version = document.createElement("span");
            version.setAttribute("class", "jenkins-description");
            version.setAttribute("style", "margin-left: 1ch;");
            version.appendChild(document.createTextNode(plugins[pluginName].version));
            li.appendChild(version);

            const details = document.createElement("details");
            const summary = document.createElement("summary");
            const counter = document.createElement("span");
            counter.setAttribute("class", "counter");
            counter.innerText = 0;

            const jobs = document.createElement("span");
            jobs.appendChild(document.createTextNode(" Jobs"));

            summary.appendChild(counter);
            summary.appendChild(jobs);

            details.appendChild(summary);
            details.appendChild(document.createElement("ul"));

            li.appendChild(details);
            factors.appendChild(li);
        }


        const pluginNode = document.getElementById(pluginName);
        const counter = pluginNode.getElementsBySelector(".counter")[0]
        const jobUl = pluginNode.getElementsByTagName("ul")[0]
        jobUl.setAttribute("style", "columns: 5");

        counter.innerText = parseInt(counter.innerText) + jobNames.length;

        for(var j = 0; jobNames.length > j; j++){
            const jobName = jobNames[j];
            const jobLi = document.createElement("li");
            const a = document.createElement("a");
            a.setAttribute("href", jobs[jobName].url);
            a.appendChild(document.createTextNode(jobs[jobName].fullDisplayName));
            jobLi.appendChild(a);
            jobUl.appendChild(jobLi);
        }

        [...jobUl.children]
          .sort((a, b) => a.innerText.localeCompare(b.innerText, undefined, {numeric: true, sensitivity: 'base'}))
          .forEach(node => jobUl.appendChild(node));
    }

    [...factors.children]
      .sort((a, b) => a.children[0].innerText.localeCompare(b.children[0].innerText, undefined, {numeric: true, sensitivity: 'base'}))
      .forEach(node => factors.appendChild(node));
}
