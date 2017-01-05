package de.steffensky.plugin.gradle.latex

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.GradleBuild


/**
 * Created by steffensky on 03.01.17.
 */
class LaTeXPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create("latex", LaTeXPluginExtension)

        def pdflatex = "pdflatex"
        def bibtex = "bibtex"

        project.task("pdfLaTeX", type: Exec) {
            doFirst {
                println "PdfLaTeX $project.latex.jobname"
                commandLine = [pdflatex] + (project.latex.pdflatexargs as List) + ["-jobname=$project.latex.jobname", "-output-directory=$project.latex.cookDir"] + [project.latex.documentBase + '.tex']
                println "============================"
            }

            outputs.upToDateWhen { false }

            doLast {
                println "PdfLaTeX done."
            }
        }

        project.task('bibtex', type: Exec) {
            doFirst {
                println "bibtex $project.latex.jobname"
                println "============================"
                commandLine = [bibtex] + [project.latex.cookDir + "/" + project.latex.jobname + '.aux']
            }

            outputs.upToDateWhen { false }

            doLast {
                println "bibtex done."
            }
        }

        project.tasks.findByName("bibtex").dependsOn {
            project.tasks.findByName("pdfLaTeX")
        }

        project.task("cleanup", type: Delete) {
            doFirst {
                println "latex cleanup"
                println "============================"
                delete project.latex.cookDir + "/" + project.latex.jobname + ".aux", project.latex.cookDir + "/" + project.latex.jobname + ".bbl", project.latex.cookDir + "/" + project.latex.jobname + ".blg", project.latex.cookDir + "/" + project.latex.jobname + ".log", project.latex.cookDir + "/" + project.latex.jobname + ".synctex.gz"
            }
            doLast {
                println "latex cleanup done"
            }
        }
        project.tasks.findByName("cleanup").mustRunAfter(project.tasks.findByName("pdfLaTeX"))

        project.task("wipe", type: Delete) {
            doFirst {
                println "latex wipe clean"
                println "============================"
                delete project.latex.cookDir + "/" + project.latex.jobname + ".pdf"
            }
            doLast {
                println "latex wipe done"
            }
        }
        project.tasks.findByName("wipe").dependsOn {
            project.tasks.findByName("cleanup")
        }


        project.task("fullbuildlatex1", type: GradleBuild) {
            tasks = ["pdfLaTeX"]
        }
        project.tasks.findByName("fullbuildlatex1").dependsOn {
            project.tasks.findByName("bibtex")
        }

        project.task("fullbuild", type: GradleBuild) {
            tasks = ["pdfLaTeX", "cleanup"]
        }
        project.tasks.findByName("fullbuild").dependsOn {
            project.tasks.findByName("fullbuildlatex1")
        }
    }
}

class LaTeXPluginExtension {
    String documentBase
    String jobname
    String[] pdflatexargs
    String cookDir
}
